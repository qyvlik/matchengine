package io.github.qyvlik.matchengine.utils;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * Created by qyvlik on 2017/6/8.
 */
public class Collections3 {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    // 分组
    public static <T> List<List<T>> group(List<T> list, Comparator<T> comparator) {
        List<List<T>> groupList = Lists.newArrayList();
        if (isEmpty(list)) {
            return groupList;
        }
        list.removeAll(Collections.singleton(null));
        while (!list.isEmpty()) {
            List<T> group = Lists.newArrayList();
            for (T item : list) {
                if (item == null) {
                    continue;
                }
                if (group.isEmpty()) {
                    group.add(item);
                } else {
                    if (comparator.compare(group.get(0), item) == 0) {
                        group.add(item);
                    }
                }
            }
            list.removeAll(group);
            if (Collections3.isNotEmpty(group)) {
                groupList.add(group);
            }
        }
        return groupList;
    }

    public static <T> List<List<T>> split(List<T> list, int step) {
        List<List<T>> splitList = Lists.newLinkedList();

        if (isEmpty(list)) {
            return splitList;
        }

        int listSize = list.size();

        if (listSize <= step) {
            splitList.add(list);
            return splitList;
        }

        int buffer = 0;
        List<T> item0 = Lists.newLinkedList();
        for (T e : list) {
            if (buffer >= step) {
                splitList.add(item0);
                item0 = Lists.newLinkedList();
                buffer = 0;
            }

            item0.add(e);
            buffer += 1;
        }

        if (isNotEmpty(item0)) {
            splitList.add(item0);
        }

        return splitList;
    }

    // such as : `[0, 1], [2, 3], [4, 4]`
    public static List<List<Long>> splitRangeToList(long start, long end, int size) {
        List<List<Long>> itemList = Lists.newLinkedList();
        if (start >= end) {
            return Lists.newArrayList();
        }

        int indexGap = size - 1;

        if (end - start < size) {
            itemList.add(Lists.newArrayList(start, end));
            return itemList;
        }

        long startIndex = start;
        long endIndex = start + indexGap;

        while (endIndex < end) {
            itemList.add(Lists.newArrayList(startIndex, endIndex));

            startIndex = endIndex + 1;

            if (startIndex > end) {
                itemList.add(Lists.newArrayList(end, end));
                break;
            }

            endIndex = startIndex + indexGap;

            if (endIndex > end) {
                itemList.add(Lists.newArrayList(startIndex, end));
                break;
            }
        }
        return itemList;
    }

    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList();
        int it = 10;
        while (it-- > 0) {
            list.add(it);
        }
        System.out.println("fill finished");

        List<List<Integer>> group = split(list, 9);
        System.out.println("list:" + list);
        System.out.println("group size:" + group.size());
        System.out.println("group : " + group);


        List<List<Long>> list1 =
                splitRangeToList(1547395200000L, 1547481600000L, 60 * 60 * 1000);
        System.out.println("list1:" + list1 + " size:" + list1.size());


        List<List<Long>> list2 =
                splitRangeToList(0, 1, 60 * 60 * 1000);
        System.out.println("list2:" + list2 + " list2:" + list2.size());

        List<List<Long>> list3 =
                splitRangeToList(-1, 1, 60 * 60 * 1000);
        System.out.println("list3:" + list3 + " list3:" + list3.size());

    }
}
