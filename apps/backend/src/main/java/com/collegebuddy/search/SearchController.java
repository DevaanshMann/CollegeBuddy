package com.collegebuddy.search;

import com.collegebuddy.dto.SearchRequest;
import com.collegebuddy.dto.SearchResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Campus-scoped student search
 */

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<SearchResultDto> search(@RequestBody SearchRequest searchRequest) {
//        TODO: enforce same-campus + visibility
        return ResponseEntity.ok(new SearchResultDto());
    }
}
