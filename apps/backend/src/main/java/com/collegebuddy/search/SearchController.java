package com.collegebuddy.search;

import com.collegebuddy.dto.SearchRequest;
import com.collegebuddy.dto.SearchResultDto;
import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<SearchResultDto> search(@RequestBody SearchRequest request) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        SearchResultDto result = searchService.searchCampusDirectory(
                current.campusDomain(),
                current.id(),
                request
        );
        return ResponseEntity.ok(result);
    }
}
