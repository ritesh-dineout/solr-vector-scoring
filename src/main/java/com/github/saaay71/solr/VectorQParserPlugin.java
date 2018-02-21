package com.github.saaay71.solr;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

public class VectorQParserPlugin extends QParserPlugin {
    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new QParser(qstr, localParams, params, req) {
            @Override
            public Query parse() throws SyntaxError {
                String field = localParams.get(QueryParsing.F);
                String vector = localParams.get("vector");
                boolean cosine = localParams.getBool("cosine", true);

                if (field == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'f' not specified");
                }

                if (vector == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "vector missing");
                }

                Query subQuery = subQuery(localParams.get(QueryParsing.V), null).getQuery();

                FieldType ft = req.getCore().getLatestSchema().getFieldType(field);
                if (ft != null) {
                    com.github.saaay71.solr.VectorQuery q = new com.github.saaay71.solr.VectorQuery(subQuery);
                    q.setQueryString(localParams.toLocalParamsString());
                    query = q;
                }


                if (query == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Query is null");
                }

                return new com.github.saaay71.solr.VectorScoreQuery(query, vector, field, cosine);

            }
        };
    }
}
