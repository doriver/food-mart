# 창고형 식자재 마트
* [재고관리+주문 시스템] 의 구현을 목표 
* 이전에 작업했던 주문API를 이식하고, 시스템으로 확장
  * https://github.com/doriver/mini
* 기술스택 : SpringBoot, MySQL
* 외부 연동없이, DB안에서 모든 흐름이 닫히도록 설계

해당 README는 작성중(미완성)입니다.

### 주문 (결제까지 하는경우)
<details>
  <summary>프로세스</summary>
  <div>
    <ol>
      <li> 고객이 주문
      </li>
      <li> 장바구니 아이템들 구매가능한지 판단(돈, 재고)   
      </li>
      <li> 주문, 주문 아이템들 생성
      </li>
      <li> 결제(돈 차감, 마트 장부 기록)
      </li>
      <li> 배송 대기상태로(재고 차감, 오더피킹 생성)
      </li>
      <li> (창고직원이 재고 피킹 예정)
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>최종 데이터 변화</summary>
  <div>
    <ol>
      <li> Order, OrderItem 생성
      </li>
      <li> Wallet에서 돈 차감, ShopLedgerHistory (입금)생성    
      </li>
      <li> Order 상태 업데이트
      </li>
      <li> Stock에서 개수 차감,  Picking 생성
      </li>
    </ol>
  </div>
</details>

주문기능 : [OrderService.java](https://github.com/doriver/food-mart/blob/47321633b10422cabf2a50dc6e70fb6e5a63da7b/src/main/java/com/example/food_mart/modules/order/application/OrderService.java#L27)

### 입고


### 출고
