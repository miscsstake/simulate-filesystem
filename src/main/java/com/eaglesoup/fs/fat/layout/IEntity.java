package com.eaglesoup.fs.fat.layout;


import com.eaglesoup.fs.fat.Layout;

public interface IEntity {
    default boolean isLFN(byte attr) {
        return (Layout.LFN_MARK & attr) != 0;
    }

    default boolean isDirEntity(byte attr) {
        return !isLFN(attr);
    }
}
