package kr.hhplus.be.server.domain.payment;

public enum PaymentStatus {

    INITIATED {
        @Override public boolean canMarkSuccess() { return true; }
        @Override public boolean canMarkFailure() { return true; }
    },
    PENDING {
        @Override public boolean canMarkSuccess() { return true; }
        @Override public boolean canMarkFailure() { return true; }
    },
    SUCCESS {
        @Override public boolean canMarkSuccess() { return false; }
        @Override public boolean canMarkFailure() { return false; }
    },
    FAILURE {
        @Override public boolean canMarkSuccess() { return false; }
        @Override public boolean canMarkFailure() { return false; }
    },
    CANCELLED {
        @Override public boolean canMarkSuccess() { return false; }
        @Override public boolean canMarkFailure() { return false; }
    };

    public abstract boolean canMarkSuccess();
    public abstract boolean canMarkFailure();
}
