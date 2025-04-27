package kr.hhplus.be.server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}