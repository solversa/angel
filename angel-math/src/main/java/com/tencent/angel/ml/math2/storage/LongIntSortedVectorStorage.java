package com.tencent.angel.ml.math2.storage;

import java.util.Arrays;

import com.tencent.angel.ml.math2.utils.RowType;
import com.tencent.angel.ml.math2.utils.ArrayCopy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class LongIntSortedVectorStorage implements LongIntVectorStorage {
  private long[] indices;
  private int[] values;
  private byte flag; // 001: dense; 010: sparse; 100: sorted
  private int size;
  private long dim;

  public LongIntSortedVectorStorage() {
    super();
  }

  public LongIntSortedVectorStorage(long dim, int size, long[] indices, int[] values) {
    this.flag = 4;
    this.dim = dim;
    this.size = size;
    this.indices = indices;
    this.values = values;
  }

  public LongIntSortedVectorStorage(long dim, long[] indices, int[] values) {
    this(dim, indices.length, indices, values);
  }

  public LongIntSortedVectorStorage(long dim, int capacity) {
    this(dim, 0, new long[capacity], new int[capacity]);
  }

  public LongIntSortedVectorStorage(long dim) {
    this(dim, (int)Math.min(64, Math.max(dim, 0)));
  }

  @Override public int get(long idx) {
    if (idx < 0 || idx > dim - 1) {
      throw new ArrayIndexOutOfBoundsException();
    } else if (size == 0 || idx > indices[size - 1] || idx < indices[0]) {
      return 0;
    } else {
      int i = Arrays.binarySearch(indices, idx);
      return i >= 0 ? values[i] : 0;
    }
  }

  @Override public void set(long idx, int value) {
    if (idx < 0 || idx > dim - 1) {
      throw new ArrayIndexOutOfBoundsException();
    }

    // 1. find the insert point
    int point;
    if (size == 0 || idx < indices[0]) {
      point = 0;
    } else if (idx > indices[size - 1]) {
      point = size;
    } else {
      point = Arrays.binarySearch(indices, idx);
      if (point >= 0) {
        values[point] = value;
        return;
      } else {
        point = -(point + 1);
      }
    }

    // 2. check the capacity and insert
    if (size == indices.length) {
      long[] newIdxs = new long[(int) (indices.length * 1.5)];
      int[] newValues = new int[(int) (indices.length * 1.5)];
      if (point == 0) {
        System.arraycopy(indices, 0, newIdxs, 1, size);
        System.arraycopy(values, 0, newValues, 1, size);
      } else if (point == size) {
        System.arraycopy(indices, 0, newIdxs, 0, size);
        System.arraycopy(values, 0, newValues, 0, size);
      } else {
        System.arraycopy(indices, 0, newIdxs, 0, point);
        System.arraycopy(values, 0, newValues, 0, point);
        System.arraycopy(indices, point, newIdxs, point + 1, size - point);
        System.arraycopy(values, point, newValues, point + 1, size - point);
      }
      newIdxs[point] = idx;
      newValues[point] = value;
      indices = newIdxs;
      values = newValues;
    } else {
      if (point != size) {
        System.arraycopy(indices, point, indices, point + 1, size - point);
        System.arraycopy(values, point, values, point + 1, size - point);
      }
      indices[point] = idx;
      values[point] = value;
    }

    // 3. increase size
    size++;
  }

  @Override public LongIntVectorStorage clone() {
    return new LongIntSortedVectorStorage(dim, size, ArrayCopy.copy(indices),
      ArrayCopy.copy(values));
  }

  @Override public LongIntVectorStorage copy() {
    return new LongIntSortedVectorStorage(dim, size, ArrayCopy.copy(indices),
      ArrayCopy.copy(values));
  }


  @Override public LongIntVectorStorage oneLikeSparse() {
    int[] oneLikeValues = new int[size];
    for (int i = 0; i < size; i++) {
      oneLikeValues[i] = 1;
    }
    return new LongIntSparseVectorStorage(dim, indices, oneLikeValues);
  }

  @Override public LongIntVectorStorage oneLikeSorted() {
    int[] oneLikeValues = new int[size];
    for (int i = 0; i < size; i++) {
      oneLikeValues[i] = 1;
    }
    return new LongIntSparseVectorStorage(dim, indices, oneLikeValues);
  }


  @Override public LongIntVectorStorage oneLikeSparse(long dim, int capacity) {
    int[] oneLikeValues = new int[capacity];
    long[] indices = new long[capacity];
    HashSet set = new HashSet<Integer>();
    Random rand = new Random();
    int j = 0;
    while (set.size() < capacity) {
      int idx = rand.nextInt((int) dim);
      if (!set.contains(idx)) {
        indices[j] = idx;
        set.add(idx);
        j++;
      }
    }
    for (int i = 0; i < capacity; i++) {
      oneLikeValues[i] = 1;
    }
    return new LongIntSparseVectorStorage(dim, indices, oneLikeValues);
  }

  @Override public LongIntVectorStorage oneLikeSorted(long dim, int capacity) {
    int[] oneLikeValues = new int[capacity];
    long[] indices = new long[capacity];
    HashSet set = new HashSet<Integer>();
    Random rand = new Random();
    int j = 0;
    while (set.size() < capacity) {
      int idx = rand.nextInt((int) dim);
      if (!set.contains(idx)) {
        indices[j] = idx;
        set.add(idx);
        j++;
      }
    }
    Arrays.sort(indices);
    for (int i = 0; i < capacity; i++) {
      oneLikeValues[i] = 1;
    }
    return new LongIntSparseVectorStorage(dim, indices, oneLikeValues);
  }

  @Override public LongIntVectorStorage oneLikeSparse(int capacity) {
    int[] oneLikeValues = new int[capacity];
    long[] indices = new long[capacity];
    HashSet set = new HashSet<Integer>();
    Random rand = new Random();
    int j = 0;
    while (set.size() < capacity) {
      int idx = rand.nextInt((int) dim);
      if (!set.contains(idx)) {
        indices[j] = idx;
        set.add(idx);
        j++;
      }
    }
    for (int i = 0; i < capacity; i++) {
      oneLikeValues[i] = 1;
    }
    return new LongIntSparseVectorStorage(dim, indices, oneLikeValues);
  }

  @Override public LongIntVectorStorage oneLikeSorted(int capacity) {
    int[] oneLikeValues = new int[capacity];
    long[] indices = new long[capacity];
    HashSet set = new HashSet<Integer>();
    Random rand = new Random();
    int j = 0;
    while (set.size() < capacity) {
      int idx = rand.nextInt((int) dim);
      if (!set.contains(idx)) {
        indices[j] = idx;
        set.add(idx);
        j++;
      }
    }
    Arrays.sort(indices);
    for (int i = 0; i < capacity; i++) {
      oneLikeValues[i] = 1;
    }
    return new LongIntSparseVectorStorage(dim, indices, oneLikeValues);
  }


  @Override public LongIntVectorStorage emptySparse() {
    return new LongIntSparseVectorStorage(dim, indices.length);
  }

  @Override public LongIntVectorStorage emptySorted() {
    return new LongIntSortedVectorStorage(dim, indices.length);
  }


  @Override public LongIntVectorStorage emptySparse(long dim, int capacity) {
    return new LongIntSparseVectorStorage(dim, capacity);
  }

  @Override public LongIntVectorStorage emptySorted(long dim, int capacity) {
    return new LongIntSortedVectorStorage(dim, capacity);
  }

  @Override public LongIntVectorStorage emptySparse(int capacity) {
    return new LongIntSparseVectorStorage(dim, capacity);
  }

  @Override public LongIntVectorStorage emptySorted(int capacity) {
    return new LongIntSortedVectorStorage(dim, capacity);
  }

  @Override public long[] getIndices() {
    return indices;
  }

  @Override public int size() {
    return size;
  }

  @Override public boolean hasKey(long key) {
    return (size != 0 && key <= indices[size - 1] && key >= indices[0]
      && Arrays.binarySearch(indices, key) > 0);
  }

  @Override public RowType getType() {
    return RowType.T_INT_SPARSE_LONGKEY;
  }

  @Override public boolean isDense() {
    return flag == 1;
  }

  @Override public boolean isSparse() {
    return flag == 2;
  }

  @Override public boolean isSorted() {
    return flag == 4;
  }

  @Override public void clear() {
    Arrays.parallelSetAll(indices, (int value) -> 0);
    Arrays.parallelSetAll(values, (int value) -> 0);
  }

  @Override public int[] getValues() {
    return values;
  }
}
