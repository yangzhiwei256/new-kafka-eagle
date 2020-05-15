package org.smartloli.kafka.eagle.web.constant;

public enum OperateSystem {
    OS_NAME("os.name"),
    LINUX("Linux");

    private final String value;

    OperateSystem(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}