package net.jrouter;

/**
 * 提供设置{@link ActionFactory}的接口。
 */
public interface ActionFactoryAware {

    /**
     * Set {@link ActionFactory}.
     *
     * @param actionFactory ActionFactory object.
     */
    void setActionFactory(ActionFactory actionFactory);
}
