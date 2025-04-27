package kr.hhplus.be.server.domain.order;

public enum OrderStatus {
    CREATED {
        @Override public boolean canCancel() { return true; }
        @Override public boolean canConfirm() { return true; }
    },
    CONFIRMED {
        @Override public boolean canCancel() { return false; }
        @Override public boolean canConfirm() { return false; }
    },
    CANCELLED {
        @Override public boolean canCancel() { return false; }
        @Override public boolean canConfirm() { return false; }
    };

    public abstract boolean canCancel();
    public abstract boolean canConfirm();
}
