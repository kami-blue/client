// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils
{
    private ArrayUtils() {
    }
    
    public static <E> List<E> toUnmodifiableList(final E... elements) {
        return (elements.length == 0) ? Collections.emptyList() : new UnmodifiableArrayList<E>(elements);
    }
    
    public static <E> List<E> toUnmodifiableCompositeList(final E[] array1, final E[] array2) {
        List<E> result;
        if (array1.length == 0) {
            result = toUnmodifiableList(array2);
        }
        else if (array2.length == 0) {
            result = toUnmodifiableList(array1);
        }
        else {
            result = new CompositeUnmodifiableArrayList<E>(array1, array2);
        }
        return result;
    }
    
    private static class UnmodifiableArrayList<E> extends AbstractList<E>
    {
        private final E[] array;
        
        UnmodifiableArrayList(final E[] array) {
            this.array = array;
        }
        
        @Override
        public E get(final int index) {
            if (index >= this.array.length) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size());
            }
            return this.array[index];
        }
        
        @Override
        public int size() {
            return this.array.length;
        }
    }
    
    private static class CompositeUnmodifiableArrayList<E> extends AbstractList<E>
    {
        private final E[] array1;
        private final E[] array2;
        
        CompositeUnmodifiableArrayList(final E[] array1, final E[] array2) {
            this.array1 = array1;
            this.array2 = array2;
        }
        
        @Override
        public E get(final int index) {
            E element;
            if (index < this.array1.length) {
                element = this.array1[index];
            }
            else {
                if (index - this.array1.length >= this.array2.length) {
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size());
                }
                element = this.array2[index - this.array1.length];
            }
            return element;
        }
        
        @Override
        public int size() {
            return this.array1.length + this.array2.length;
        }
    }
}
