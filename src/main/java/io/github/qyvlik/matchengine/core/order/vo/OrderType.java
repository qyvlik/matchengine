package io.github.qyvlik.matchengine.core.order.vo;

public enum  OrderType {
    unknown {
        @Override
        public boolean isSell() {
            return false;
        }

        @Override
        public boolean isBuy() {
            return false;
        }

        @Override
        public boolean isLimit() {
            return false;
        }
    },
    limitBuy {
        @Override
        public boolean isSell() {
            return false;
        }

        @Override
        public boolean isBuy() {
            return true;
        }

        @Override
        public boolean isLimit() {
            return true;
        }
    },
    limitSell {
        @Override
        public boolean isSell() {
            return true;
        }

        @Override
        public boolean isBuy() {
            return false;
        }

        @Override
        public boolean isLimit() {
            return true;
        }
    },
    marketBuy {
        @Override
        public boolean isSell() {
            return false;
        }

        @Override
        public boolean isBuy() {
            return true;
        }

        @Override
        public boolean isLimit() {
            return false;
        }
    },
    marketSell {
        @Override
        public boolean isSell() {
            return true;
        }

        @Override
        public boolean isBuy() {
            return false;
        }

        @Override
        public boolean isLimit() {
            return false;
        }
    };

    public abstract boolean isSell();

    public abstract boolean isBuy();

    public abstract boolean isLimit();
}
