package com.collegebuddy.search;

import org.springframework.stereotype.Service;

/*
    Abstraction for whatever search/index backend you use.
 */

@Service
public class IndexService {

    public Object queryByNameOrUsername(String campusDomain, String q){
//        TODO: real lookup
        return null;
    }
}
