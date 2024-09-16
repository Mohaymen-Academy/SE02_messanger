package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.util.*;

public class UserSearch extends SearchIndex {

    static final String INDEX_NAME = "/UserIndex";

    @SneakyThrows
    public UserSearch() {
        super(INDEX_NAME, createAnalyzer());
    }

    private static Analyzer createAnalyzer() {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put(FieldNameLucene.PROFILE_ID, new KeywordAnalyzer());
        analyzerMap.put(FieldNameLucene.EMAIL, new CustomHandleAnalyzer());
        analyzerMap.put(FieldNameLucene.HANDLE, new CustomHandleAnalyzer());
        return new PerFieldAnalyzerWrapper(new CustomAnalyzer(), analyzerMap);
    }

    private Document createDocument(String profileId,
                                    String email,
                                    String handle) {
        Document document = new Document();
        document.add(new TextField(FieldNameLucene.PROFILE_ID, profileId, Field.Store.YES));
        document.add(new TextField(FieldNameLucene.EMAIL, email, Field.Store.YES));
        document.add(new TextField(FieldNameLucene.HANDLE, handle, Field.Store.YES));
        return document;
    }

    public void indexUserDocument(String profileId,
                                  String email,
                                  String handle) {
        Document document = createDocument(profileId, email, handle);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUser(String profileId,
                           String handle) {
        String email = null;
        Term idTerm = new Term(FieldNameLucene.PROFILE_ID,
                analyzer.normalize(FieldNameLucene.PROFILE_ID, profileId));
        Query idQuery = new TermQuery(idTerm);
        try {
            List<Document> documents = searchIndexQuery(idQuery, 1);
            email = documents.get(0).get(FieldNameLucene.EMAIL);
        } catch (IOException ignore) {
        }
        Document document = createDocument(profileId, email, handle);
        try {
            updateDocument(
                    new Term(FieldNameLucene.PROFILE_ID,
                            analyzer.normalize(FieldNameLucene.PROFILE_ID, profileId)),
                    document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(String profileId) {
        Query query = new TermQuery(
                new Term(FieldNameLucene.PROFILE_ID,
                        analyzer.normalize(FieldNameLucene.PROFILE_ID, profileId)));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllUsers(String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new PrefixQuery(new Term(FieldNameLucene.EMAIL,
                                analyzer.normalize(FieldNameLucene.EMAIL, queryString))),
                        BooleanClause.Occur.SHOULD)
                .add(new PrefixQuery(new Term(FieldNameLucene.HANDLE,
                                analyzer.normalize(FieldNameLucene.HANDLE, queryString))),
                        BooleanClause.Occur.SHOULD)
                .build();
        try {
            return searchIndexQuery(booleanQuery, 3);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}
