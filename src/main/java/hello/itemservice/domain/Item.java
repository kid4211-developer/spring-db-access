package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * @Entity : JPA 가 사용하는 객체를 의미한다. 해당 애노케이션이 붙은 객체를 엔티티라고 한다.
 * @Table : 클래스명과 실제 DB 테이블의 명칭이 동일하다면 생략이 가능
 */
@Data
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY: DB 에서 값을 넣어주는 방식
    private Long id;

    @Column(name = "item_name", length = 10)
    private String itemName;

    @Column(name = "price")
    private Integer price;

    @Column(name = "quantity")
    private Integer quantity;

    // JPA 는 public or protected 의 기본 생성자가 필수
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
