package com.haoyou.service.kb.dto;

import java.util.List;

/** 简单分页包装 */
public class PageVO<T> {

    private long total;
    private List<T> list;

    public PageVO(long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
}
