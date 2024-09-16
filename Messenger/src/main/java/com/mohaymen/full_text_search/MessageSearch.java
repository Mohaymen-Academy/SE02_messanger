package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.util.*;

public class MessageSearch extends SearchIndex {

    static final String INDEX_NAME = "/MessageIndex";

    @SneakyThrows
    public MessageSearch() {
        super(INDEX_NAME, createAnalyzer());
    }

    private static Analyzer createAnalyzer() {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put(FieldNameLucene.SENDER_ID, new KeywordAnalyzer());
        analyzerMap.put(FieldNameLucene.RECEIVER_ID, new KeywordAnalyzer());
        analyzerMap.put(FieldNameLucene.MESSAGE_iD, new KeywordAnalyzer());
        analyzerMap.put(FieldNameLucene.MESSAGE_TEXT, new CustomAnalyzer());
        return new PerFieldAnalyzerWrapper(new CustomAnalyzer(), analyzerMap);
    }

    private Document createDocument(String messageId,
                                    String senderProfileId,
                                    String receiverProfileId,
                                    String messageText) {
        Document document = new Document();
        document.add(new TextField(FieldNameLucene.SENDER_ID, senderProfileId, Field.Store.YES));
        document.add(new TextField(FieldNameLucene.RECEIVER_ID, receiverProfileId, Field.Store.YES));
        document.add(new TextField(FieldNameLucene.MESSAGE_iD, messageId, Field.Store.YES));
        document.add(new TextField(FieldNameLucene.MESSAGE_TEXT, messageText, Field.Store.YES));
        return document;
    }

    public void indexMessageDocument(String messageId,
                                     String senderProfileId,
                                     String receiverProfileId,
                                     String messageText) {
        Document document = createDocument(messageId, senderProfileId, receiverProfileId, messageText);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMessage(String messageId,
                              String senderProfileId,
                              String receiverProfileId,
                              String messageText) {
        Document document = createDocument(messageId, senderProfileId, receiverProfileId, messageText);
        try {
            updateDocument(new Term(FieldNameLucene.MESSAGE_iD,
                            analyzer.normalize(FieldNameLucene.MESSAGE_iD, messageId)),
                    document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(String messageId) {
        Query query = new TermQuery(
                new Term(FieldNameLucene.MESSAGE_iD,
                        analyzer.normalize(FieldNameLucene.MESSAGE_iD, messageId)));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllMessages(String senderProfileId,
                                              List<String> receiverPvIds,
                                              List<String> receiverChatIds,
                                              String queryString) {
        BooleanQuery.Builder finalBooleanQuery = new BooleanQuery.Builder();

        if(queryString.length() < 3){
            finalBooleanQuery.add(new TermQuery(
                            new Term(FieldNameLucene.MESSAGE_TEXT,
                                    analyzer.normalize(FieldNameLucene.MESSAGE_TEXT, queryString))),
                    BooleanClause.Occur.MUST);
        }
        else{
            finalBooleanQuery.add(getSearchEntryTextQuery(queryString),
                    BooleanClause.Occur.MUST);
        }

        BooleanQuery.Builder idBooleanQuery = new BooleanQuery.Builder();

        for (String id : receiverPvIds) {
            idBooleanQuery.add(
                    getPvIdQuery(senderProfileId, id),
                    BooleanClause.Occur.SHOULD);
        }
        for (String id : receiverChatIds) {
            idBooleanQuery.add(new TermQuery(
                            new Term(FieldNameLucene.RECEIVER_ID,
                                    analyzer.normalize(FieldNameLucene.RECEIVER_ID, id))),
                    BooleanClause.Occur.SHOULD);
        }

        finalBooleanQuery.add(idBooleanQuery.build(),
                BooleanClause.Occur.MUST);

        try {
            return searchIndexQuery(finalBooleanQuery.build(), 20);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<Document> searchInPv(String senderProfileId,
                                     String receiverProfileId,
                                     String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(getSearchEntryTextQuery(queryString),
                        BooleanClause.Occur.MUST)
                .add(getPvIdQuery(senderProfileId, receiverProfileId),
                        BooleanClause.Occur.MUST)
                .build();

        try {
            return searchIndexQuery(booleanQuery, 20);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<Document> searchInChat(String receiverProfileId, String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(getSearchEntryTextQuery(queryString),
                        BooleanClause.Occur.MUST)
                .add(new TermQuery(
                                new Term(FieldNameLucene.RECEIVER_ID,
                                        analyzer.normalize(FieldNameLucene.RECEIVER_ID, receiverProfileId))),
                        BooleanClause.Occur.MUST)
                .build();

        try {
            return searchIndexQuery(booleanQuery, 20);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private BooleanQuery getSearchEntryTextQuery(String queryString) {
        BooleanQuery.Builder searchEntryBooleanQuery = new BooleanQuery.Builder();

        TokenStream stream = analyzer.tokenStream(FieldNameLucene.MESSAGE_TEXT, queryString);
        try {
            stream.reset();
            while (stream.incrementToken()) {
                searchEntryBooleanQuery.add(new FuzzyQuery(
                                new Term(FieldNameLucene.MESSAGE_TEXT,
                                        analyzer.normalize(FieldNameLucene.MESSAGE_TEXT, stream.getAttribute(CharTermAttribute.class).toString())), 1),
                        BooleanClause.Occur.MUST);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }

        return searchEntryBooleanQuery.build();
    }

    private BooleanQuery getPvIdQuery(String senderProfileId,
                                      String receiverProfileId) {
        BooleanQuery idQuerySenderReceiver = new BooleanQuery.Builder()
                .add(new TermQuery(
                                new Term(FieldNameLucene.SENDER_ID,
                                        analyzer.normalize(FieldNameLucene.SENDER_ID, senderProfileId))),
                        BooleanClause.Occur.MUST)
                .add(new TermQuery(
                                new Term(FieldNameLucene.RECEIVER_ID,
                                        analyzer.normalize(FieldNameLucene.RECEIVER_ID, receiverProfileId))),
                        BooleanClause.Occur.MUST)
                .build();

        BooleanQuery idQueryReceiverSender = new BooleanQuery.Builder()
                .add(new TermQuery(
                                new Term(FieldNameLucene.SENDER_ID,
                                        analyzer.normalize(FieldNameLucene.SENDER_ID, receiverProfileId))),
                        BooleanClause.Occur.MUST)
                .add(new TermQuery(
                                new Term(FieldNameLucene.RECEIVER_ID,
                                        analyzer.normalize(FieldNameLucene.RECEIVER_ID, senderProfileId))),
                        BooleanClause.Occur.MUST)
                .build();

        return new BooleanQuery.Builder()
                .add(idQuerySenderReceiver, BooleanClause.Occur.SHOULD)
                .add(idQueryReceiverSender, BooleanClause.Occur.SHOULD)
                .build();
    }

}
