package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;

import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional
public class JpaItemRepositoryV1 implements ItemRepository {
    /**
     * @EntityManager
     * - JPA 의 모든 동작은 엔티티 매니저를 통해서 이루어진다.
     * - 엔티티 매니저 는 내부에 데이터소스를 가지고 있고, 데이터베이스에 접근할 수 있다.
     */
    private final EntityManager entityManager;

    public JpaItemRepositoryV1(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Item save(Item item) {
        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = entityManager.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        /**
         * @jpql - Java Persistence Query Language
         */
        String jpql = "select i from Item i";
        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }
        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }

        log.info("jpql={}", jpql);

        TypedQuery<Item> typedQuery = entityManager.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            typedQuery.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            typedQuery.setParameter("maxPrice", maxPrice);
        }

        return typedQuery.getResultList();
    }
}