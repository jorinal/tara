package com.sondertara.common.bean.copier;

/**
 * A copier generated by the BeanCopy analyzer.
 *
 * @author huangxiaohu
 */
public interface Copier {

     static CopyThreadLocal COPY_IGNORE_NULL = new CopyThreadLocal();

    /**
     * copy
     *
     * @param source source object
     * @param target target object
     */
    void copy(Object source, Object target);

    static class CopyThreadLocal extends ThreadLocal<Boolean> {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    }
}
