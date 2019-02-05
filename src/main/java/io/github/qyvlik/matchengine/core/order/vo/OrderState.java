package io.github.qyvlik.matchengine.core.order.vo;

public enum OrderState {
    submitting {
        @Override
        public boolean isFinal() {
            return false;
        }
    },
    submitted {
        @Override
        public boolean isFinal() {
            return false;
        }
    },
    partialFill {
        @Override
        public boolean isFinal() {
            return false;
        }
    },
    filled {
        @Override
        public boolean isFinal() {
            return true;
        }
    },
    partialCancel {
        @Override
        public boolean isFinal() {
            return true;
        }
    },
    cancel {
        @Override
        public boolean isFinal() {
            return true;
        }
    };

    public abstract boolean isFinal();
}
