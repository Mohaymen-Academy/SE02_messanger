package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class SearchIndex {

    static final String INDEX_DIRECTORY = "../../LuceneDirectory/";

    protected final Directory memoryIndex;

    protected Analyzer analyzer;

    @SneakyThrows
    public SearchIndex(String indexName, Analyzer analyzer) {
        memoryIndex = FSDirectory.open(Paths.get(INDEX_DIRECTORY + indexName));
        this.analyzer = analyzer;
    }

    protected List<Document> searchIndexQuery(Query query, int limit) throws IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, limit);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }
        return documents;
    }

    protected void indexDocument(Document document) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        writer.addDocument(document);
        writer.close();
    }

    protected void deleteDocument(Query query) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        writer.deleteDocuments(query);
        writer.close();
    }

    protected void updateDocument(Term term, Document document) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        writer.updateDocument(term, document);
        writer.close();
    }

}
