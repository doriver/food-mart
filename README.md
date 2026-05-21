# 창고형 식자재 마트
* [재고관리+주문 시스템] 의 구현을 목표 
* 이전에 작업했던 주문API를 이식하고, 시스템으로 확장
  * https://github.com/doriver/mini
* 기술스택 : SpringBoot, MySQL
* 외부 연동없이, DB안에서 모든 흐름이 닫히도록 설계

해당 README는 작성중(미완성)입니다.

<details>
<summary><h2>DB 관련 정책</h2></summary>

(예외상황 있을수 있음)
* FK 사용x , 참조필드에 index를 사용한다.
* JPA를 기본으로 하되    
  동적쿼리, 복잡한 쿼리등은 MyBatis를 이용한다.
* JPA 연관관계는 @ManyToOne(fetch = FetchType.LAZY) 만 사용한다.
</details>


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
      <li> ItemInCart 삭제
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

<details>
  <summary>프로세스</summary>
  <div>
    <ol>
      <li> (외부에서 물건 도착한 상황)
      </li>
      <li> 물건 받은 직원이 입고 등록 신청   
      </li>
      <li> 입고, 입고 아이템 생성
      </li>
      <li> (적재 담당자가 창고에 적재 예정)
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>최종 데이터 변화</summary>
  <div>
    <ol>
      <li> Inbound, InboundItem 생성
      </li>
    </ol>
  </div>
</details>

#### 입고된 물건 적재

<details>
  <summary>프로세스</summary>
  <div>
    <ol>
      <li> 직원이 입고된 물건들 조회
      </li>
      <li> (아이템과 자리있는 창고 매핑)   
      </li>
      <li> 창고에 아이템 적재후, 적재 완료요청 
      </li>
      <li> 추가한 재고 증가, 입고아이템 적재 완료 업데이트
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>최종 데이터 변화</summary>
  <div>
    <ol>
      <li> Stock 생성 or 개수 증가
      </li>
      <li> InboundItem 적재 상태, 적재한 직원 업데이트
      </li>
    </ol>
  </div>
</details>

### 출고