package com.lh.classannotation;

public interface ViewInjector<T> {
    void inject(T t , Object object);
}
