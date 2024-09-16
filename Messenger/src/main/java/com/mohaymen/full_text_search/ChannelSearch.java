package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.util.*;

public class ChannelSearch extends SearchIndex {

    static final String INDEX_NAME = "ChannelIndex";

    @SneakyThrows
    public ChannelSearch() {
        super(INDEX_NAME, createAnalyzer());
    }

    private static Analyzer createAnalyzer() {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put(FieldNameLucene.PROFILE_ID, new KeywordAnalyzer());
        analyzerMap.put(FieldNameLucene.NAME, new CustomAnalyzer());
        return new PerFieldAnalyzerWrapper(new CustomAnalyzer(), analyzerMap);
    }

    private Document createDocument(String profileId,
                                    String channelName) {
        Document document = new Document();
        document.add(new TextField(FieldNameLucene.PROFILE_ID, profileId, Field.Store.YES));
        document.add(new TextField(FieldNameLucene.NAME, channelName, Field.Store.YES));
        return document;
    }

    public void indexChannelDocument(String profileId,
                                     String channelName) {
        Document document = createDocument(profileId, channelName);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateChannel(String profileId,
                              String channelName) {
        Document document = createDocument(profileId, channelName);
        try {
            updateDocument(
                    new Term(FieldNameLucene.PROFILE_ID,
                            analyzer.normalize(FieldNameLucene.PROFILE_ID, profileId)),
                    document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteChannel(String profileId) {
        Query query = new TermQuery(
                new Term(FieldNameLucene.PROFILE_ID,
                        analyzer.normalize(FieldNameLucene.PROFILE_ID, profileId)));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllChannels(String queryString) {
        try {
            return searchIndexQuery(
                    new FuzzyQuery(
                            new Term(FieldNameLucene.NAME,
                                    analyzer.normalize(FieldNameLucene.NAME, queryString)), 1),
                    3);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}
