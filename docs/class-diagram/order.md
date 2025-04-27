```mermaid
classDiagram
    class Order {
        - String id
        - Long userId
        - List~OrderItem~ items
        - Money totalAmount
        - OrderStatus status
        - LocalDateTime createdAt
        + static create(...)
        + void cancel()
        + void markConfirmed()
    }

    class OrderItem {
        - Long productId
        - int quantity
        - int size
        - Money price
        + static of(...)
        + Money calculateTotal()
    }

    class OrderStatus {
        <<enumeration>>
        CREATED
        CONFIRMED
        CANCELLED
    }

    class OrderHistory {
        - Long id
        - String orderId
        - OrderStatus status
        - String memo
        - LocalDateTime changedAt
        + static recordStatus(Order, String)
    }

    class OrderEvent {
        - UUID id
        - String aggregateType
        - String eventType
        - String payload
        - String status
        - int retryCount
        - LocalDateTime createdAt
        - LocalDateTime lastAttemptedAt
        + static fromOrder(Order)
    }

    class OutboxEvent {
        - UUID id
        - String aggregateType
        - String aggregateId
        - String eventType
        - String payload
        - String status
        - int retryCount
        - LocalDateTime createdAt
        - LocalDateTime lastAttemptedAt
        + static from(OrderEvent)
    }

    class OrderFacadeService {
        - ProductService productService
        - BalanceService balanceService
        - OrderService orderService
        + OrderResult createOrder(CreateOrderCommand)
    }

    class OrderService {
        - OrderRepository orderRepository
        - OrderHistoryRepository orderHistoryRepository
        - OrderEventPublisher orderEventPublisher
        + Order createOrder(Long, List~OrderItem~, Money)
        + Order getOrderForPayment(String)
        + void markConfirmed(Order)
    }

    class OrderUseCase {
        <<interface>>
        + OrderResult createOrder(CreateOrderCommand)
    }

    class CreateOrderCommand {
        - Long userId
        - List~OrderItemCommand~ items
    }

    class OrderItemCommand {
        - Long productId
        - int quantity
        - int size
    }

    class OrderResult {
        - String orderId
        - BigDecimal totalAmount
        - String status
        + static from(Order): OrderResult
    }

    %% 관계 정의
    OrderFacadeService --> ProductService
    OrderFacadeService --> BalanceService
    OrderFacadeService --> OrderService
    OrderFacadeService --> OrderUseCase
    OrderUseCase <|.. OrderFacadeService

    OrderService --> OrderRepository
    OrderService --> OrderHistoryRepository
    OrderService --> OrderEventPublisher
    OrderService --> Order

    Order --> OrderItem
    Order --> OrderStatus
    Order --> Money
    OrderItem --> Money

    Order --> OrderHistory
    Order --> OrderEvent
    OrderEvent --> OutboxEvent

```**