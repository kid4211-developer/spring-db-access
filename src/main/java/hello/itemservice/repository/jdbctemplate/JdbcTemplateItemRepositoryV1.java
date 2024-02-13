package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * - JdbcTemplate 는 dataSource 가 필요하다.
     * - 생성자를 보면 dataSource 를 의존 관계 주입 받고 생성자 내부에서 JdbcTemplate 을 생성
     */
    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            // 자동 증가 키
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, item.getItemName());
            preparedStatement.setInt(2, item.getPrice());
            preparedStatement.setInt(3, item.getQuantity());
            return preparedStatement;
        }, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        log.info("sql={}", sql);
        jdbcTemplate.update(sql, updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(), itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = ?";
        log.info("sql={}", sql);
        try {
            /**
             * - RowMapper 는 데이터베이스의 반환 결과인 ResultSet 을 객체로 변환
             * - 결과가 없으면 EmptyResultDataAccessException 예외가 발생
             * - 결과가 둘 이상이면 IncorrectResultSizeDataAccessException 예외가 발생
             */
            Item item = jdbcTemplate.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        String sql = "select id, item_name, price, quantity from item"; // 동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);

        }
        log.info("sql={}", sql);
        return jdbcTemplate.query(sql, itemRowMapper(), param.toArray());
    }

    /**
     * - 데이터베이스의 조회 결과를 객체로 변환할 때 사용 (JDBC 를 직접 사용할 때 ResultSet 를 사용했던 부분을 떠올리면 된다)
     * - 차이가 있다면 다음과 같이 JdbcTemplate 가 다음과 같은 루프를 돌려주고, 개발자는 RowMapper 를 구현해 그 내부 코드만 채운다고 이해하면 된다.
     */
    private RowMapper<Item> itemRowMapper() {
        return (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        };
    }
}