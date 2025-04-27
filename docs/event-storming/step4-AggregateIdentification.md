## Step 4 - Aggregate Event Storming
```mermaid
flowchart LR
%% 스타일 정의
    classDef aggregate fill:#E1BEE7,stroke:#9C27B0,color:#4A148C,font-weight:bold;
    classDef command fill:#BBDEFB,stroke:#1976D2,color:#0D47A1,font-weight:bold;
    classDef event fill:#FFECB3,stroke:#FF9800,color:#E65100,font-weight:bold;
    classDef policy fill:#F5F5F5,stroke:#9E9E9E,color:#212121,font-weight:bold;
    classDef external fill:#FFCDD2,stroke:#F44336,color:#B71C1C,font-weight:bold;

%% 애그리게이트
    OrderAgg[Order 애그리게이트]:::aggregate
    ProductAgg[Product 애그리게이트]:::aggregate
    CouponAgg[Coupon 애그리게이트]:::aggregate
    BalanceAgg[Balance 애그리게이트]:::aggregate
    Outbox[(ORDER_EVENTS 테이블)]:::external
    EXT_LOGIC[(이벤트 후처리 트리거)]:::external

%% 정책
    CreateOrderPolicy[주문 생성 정책]:::policy
    CheckStockPolicy[재고 확인 정책]:::policy
    ApplyCouponPolicy[쿠폰 적용 정책]:::policy
    PaymentPolicy[결제 처리 정책]:::policy
    CompleteOrderPolicy[주문 완료 정책]:::policy
    SaveOutboxPolicy[이벤트 저장 정책]:::policy
    DispatchPolicy[이벤트 후처리 정책]:::policy

%% 이벤트
    E_OrderRequested[주문 요청이 접수되었다]:::event
    E_StockOk[상품 재고가 충분하다]:::event
    E_CouponUsed[쿠폰이 사용되었다]:::event
    E_PaymentDone[결제 금액이 차감되었다]:::event
    E_OrderCreated[주문이 생성되었다]:::event
    E_Triggered[주문 이벤트 후처리 트리거]:::event

%% 이벤트 흐름
    OrderAgg --> CreateOrderPolicy --> E_OrderRequested
    E_OrderRequested --> CheckStockPolicy --> ProductAgg --> E_StockOk
    E_StockOk --> ApplyCouponPolicy --> CouponAgg --> E_CouponUsed
    E_CouponUsed --> PaymentPolicy --> BalanceAgg --> E_PaymentDone
    E_PaymentDone --> CompleteOrderPolicy --> OrderAgg --> E_OrderCreated
    E_OrderCreated --> SaveOutboxPolicy --> Outbox
    Outbox --> DispatchPolicy --> EXT_LOGIC --> E_Triggered

%% 관계 흐름 라인
    subgraph 도메인_이벤트_흐름
        E_OrderRequested --> E_StockOk
        E_StockOk --> E_CouponUsed
        E_CouponUsed --> E_PaymentDone
        E_PaymentDone --> E_OrderCreated
        E_OrderCreated --> E_Triggered
    end

```