package com.collegebuddy.search;

import com.collegebuddy.dto.SearchRequest;
import com.collegebuddy.dto.SearchResultDto;
import org.springframework.stereotype.Service;

/*
    Applies campus filter + visibility filter and queries index
 */

@Service
public class SearchService {

    private final IndexService indexService;

    public SearchService(IndexService indexService) {
        this.indexService = indexService;
    }

    public SearchResultDto searchCampusDirectory(SearchRequest request) {
//        TODO: call indexService.query(), filter results
        return new SearchResultDto();
    }
}
