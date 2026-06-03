# 물류 이커머스
* '입고 ~ 재고 ~ 주문 ~ 출고' 전반을 다룬다. 
* 기술스택 : SpringBoot, MySQL
* 외부 연동없이, DB안에서 모든 흐름이 닫히도록 설계

해당 README는 작성중(미완성)입니다.

<details>
<summary><h2> ✅ 소프트웨어 아키텍처</h2></summary>

<img width="300" height="300" alt="image" src="img/architech.png" />

<h3>Rest API 응답 설계</h3>

'HTTP 상태코드' 에 따른 응답
* 2xx은 @controller에서
* 4xx, 5xx 은 @ExceptionHandler 쪽에서
  * 커스텀한 Expected4xxException, Expected5xxException를 api로직에서 throw함
* 응답형식은 ApiResponse클래스로 일괄 처리
  * 정적 팩토리 메서드(Static Factory Method)패턴을 사용

<h3>DB 관련 정책</h3>

(예외상황 있을수 있음)
* FK 사용x , 참조필드에 index를 사용한다.
* JPA를 기본으로 하되    
  동적쿼리, 복잡한 쿼리등은 MyBatis를 이용한다.
* JPA 연관관계는 @ManyToOne(fetch = FetchType.LAZY) 만 사용한다.

<h3>프레임워크(SpringBoot) 핸들링</h3>
<details>
  <summary>데이터 유효성 검사</summary>
  <ul>
      <li> DTO에 검증 조건(어노테이션) 추가
      </li>
      <li> Controller에서 @Valid 사용 <br>
            →  검증 실패 시 자동으로 MethodArgumentNotValidException 이 발생
      </li>
      <li> 검증 실패(에러) 응답 처리
        <ul>
          <li> @ExceptionHandler(MethodArgumentNotValidException.class) 에서 처리
          </li>
          <li> Exception → BindingResult  → FieldError  →  getField() , getDefaultMessage() , getRejectedValue()
          </li>
          <li> 데이터가 어떤식으로 잘못됐는지, 사용자에게 알린다.
          </li>
        </ul>
      </li>
      
  </ul>
</details>
<details>
  <summary>Controller 매개변수 커스텀</summary>
  <ul>
      <li> HandlerMethodArgumentResolver를 구현해, 매개변수로 넘겨줄 클래스를 커스텀한다.
      </li>
      <li> WebMvcConfigurer를 구현한 config클래스에서, 위에서 구현한 클래스를 addArgumentResolvers 해준다.
      </li>
  </ul>
</details>
<details>
  <summary>다른것도 추가 예정</summary>
  <ul>
      <li> 
      </li>
  </ul>
</details>

</details>
</details>




## 🎥 주요 기능들

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
      <li> Wallet에서 돈 차감, ShopLedgerHistory (입금)생성 <br>
          Order상태 업데이트, OrderHistory 저장
      </li>
      <li> Stock에서 개수 차감,  Picking 생성 <br>
          Order상태 업데이트, OrderHistory 저장
      </li>
    </ol>
  </div>
</details>

  주문기능 : [OrderService.java](https://github.com/doriver/food-mart/blob/47321633b10422cabf2a50dc6e70fb6e5a63da7b/src/main/java/com/example/food_mart/modules/order/application/OrderService.java#L27)

#### 주문 취소
<details>
  <summary>특징</summary>
  <ul>
      <li>주문 상태가 REGISTER, PAID, WAITDELIVERY 일때만 취소가능
      </li>
      <li> WAITDELIVERY 의 경우 3개의 트랜잭션으로 처리(나머진 트랜잭션 1개로 처리됨)
        <ol>
          <li> 출고 존재하면 취소처리 - 멱등성으로 구현
          </li>
          <li> 재고 복원 - record X-Lock , 주문 상태값 cancelling
          </li>
          <li> 환불 - 주문 상태값 cancel
          </li>
        </ol>
      </li>
      <li>pessimistic lock 포함 -> 트랜잭션 분리 <br>
          트랜잭션 분리에 따른 데이터 정합성 문제를 주문 상태값 CANCELLING 추가하여 해결
      </li>
  </ul>
</details>
<details>
  <summary>최종 데이터 변화</summary>
  <div>
    <ul>
      <li> REGISTER 인 경우
        <ol>
          <li> 주문상태 취소 - Order상태 업데이터 , OrderHistory 생성
          </li>
        </ol>
      </li>
      <li> PAID 인 경우
        <ol>
          <li> Wallet에 돈 증가, ShopLedgerHistory (환불)생성 <br>
              주문상태 취소로
          </li>
        </ol>
      </li>
      <li> WAITDELIVERY 인 경우
        <ol>
          <li> Outbound (존재하면) 상태 업데이트
          </li>
          <li> Picking 업데이트 , Stock 증가 <br>
              주문상태 Cancelling 으로
          </li>
          <li> Wallet에 돈 증가, ShopLedgerHistory (환불)생성 <br>
              주문상태 취소로
          </li>
        </ol>
      </li>
    </ul>
  </div>
</details>

#### 주문된 상품들 피킹
<details>
  <summary>프로세스</summary>
  <div>
    <ol>
      <li> (오더피킹 생성에서 주문 아이템이 재고에 매핑 된다)
      </li>
      <li> (직원이 피킹 목록을 조회하고, 대기중인 피킹들에 대해 작업한다)
      </li>
      <li> (창고에 있는 재고를 출고장소로 옮긴뒤) 피킹 완료 요청을 한다.
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>최종 데이터 변화</summary>
  <div>
    <ol>
      <li> Picking 상태와 담당직원 업데이트
      </li>
    </ol>
  </div>
</details>

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

<details>
  <summary>프로세스</summary>
  <div>
    <ol>
      <li> (직원이 출고처리 하는 장소에서, 주문에 해당하는 상품들이 다 있는지 확인)
      </li>
      <li> 주문의 도착지, 배송회사 등등 입력해서 출고등록 요청
      </li>
      <li> (출고 등록 완료됨) 
      </li>
      <li> 배송 회사가 물건 가지고가는거 확인하고, 출고 완료요청
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>최종 데이터 변화</summary>
  <div>
    <ol>
      <li> Delivery 생성
      </li>
      <li> Outbound 생성
      </li>
      <li> Outbound 상태와 담당직원 업데이트
      </li>
      <li> Order상태 업데이트, OrderHistory 저장
      </li>
    </ol>
  </div>
</details>

<h2>📄 세부 기능들</h2>
<details>
  <summary>재고 개수 변화</summary>
  <div>
    <ul>
      <li> 입고된 물건 적재후, 증가
      </li>
      <li> 주문 결제후 배송대기 상태로에서, 감소
      </li>
      <li> (배송 대기상태인) 주문 취소에서, 증가
      </li>
    </ul>
    수정하려는 재고를 PK로 for update조회해서 Record X-Lock을 획득후 개수 변경
  </div>
  <img width="500" src="img/PESSIMISTIC_WRITE.png" />
</details>
<details>
  <summary>주문 상태 관리</summary>
  <div>
    <ul>
      <li> 주문 상태 변경 제한 - 상태 enum에서 다음 상태가능한거 정의, order엔티티 상태업데이트 메소드에 해당 로직 적용
      </li>
      <img width="500" src="img/OrderStatus.png" />
      <li> 주문상태 변경이력 기록 - 주문상태 변경하면서, 같이 해당 OrderHistory(이전상태, 이후상태, 등등)을 저장한다
      </li>
      <img width="500" src="img/OrderHistory.png" />
    </ul>
  </div>
</details>

<details>
  <summary>상품들 일별 판매량 스냅샷</summary>
  <div>
    <ul>
      <li> batch프레임워크 사용
      </li>
      <img width="500" src="img/itemSalesCountBatch.png" />
      <li> 스냅샷 데이터(해당 아이템, 판매개수, 날짜) 조회
      </li>
      <img width="500" src="img/sumDailyCountByItemId.png" />
    </ul>
  </div>
</details>

<details>
  <summary>주문 진행 상태 추적 (주문 → 피킹 → 출고 → 배송)</summary>
  <ul>
      <li> 주문상태, 피킹 진행도, 출고상태, 배송관련(추가 개발 필요) 등을 조회
      </li>
      <li> 연속된 LEFT JOIN 이나, orderId 1개로 무리x
      </li>
      <li> 집계 서브쿼리가 있으나, 해당 쿼리가 읽는 row 개수가 많지 않아 무리x
      </li>
      <img width="500" src="img/findOrderFlow.png" />
  </ul>
</details>

<details>
  <summary>출고 지연 탐지</summary>
  : 주문 결제후 48시간이 지났는데, 출고가 완료되지 않은 주문들 조회
  <img width="500" src="img/findOutboundDelays.png" />
</details>