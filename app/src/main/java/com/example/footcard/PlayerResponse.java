package com.example.footcard;

import java.util.List;

public class PlayerResponse {
    private List<Player> content;
    private PageInfo page;

    // Getters and Setters
    public List<Player> getContent() {
        return content;
    }

    public void setContent(List<Player> content) {
        this.content = content;
    }

    public PageInfo getPage() {
        return page;
    }

    public void setPage(PageInfo page) {
        this.page = page;
    }

    // Inner class to represent the page information
    public static class PageInfo {
        private int size;
        private int number;
        private int totalElements;
        private int totalPages;

        // Getters and Setters
        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(int totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}
