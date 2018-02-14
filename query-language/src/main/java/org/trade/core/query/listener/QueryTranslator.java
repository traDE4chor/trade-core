/* Copyright 2018 Michael Hahn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trade.core.query.listener;

import org.trade.core.query.Query;
import org.trade.core.query.antlr.QueryBaseListener;
import org.trade.core.query.antlr.QueryParser;

/**
 * This class provides a special {@link QueryBaseListener} implementation that translates the information of a query
 * string into an {@link Query} object.
 * <p>
 * Created by hahnml on 05.02.2018.
 */
public class QueryTranslator extends QueryBaseListener {

    private Query query;

    public QueryTranslator(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public void exitData_object_reference(QueryParser.Data_object_referenceContext ctx) {
        super.exitData_object_reference(ctx);

        if (ctx.exception == null) {
            query.setDataObjectName(ctx.getText());
        }
    }

    @Override
    public void exitData_element_reference(QueryParser.Data_element_referenceContext ctx) {
        super.exitData_element_reference(ctx);

        if (ctx.exception == null) {
            query.setDataElementName(ctx.getText());
        }
    }

    @Override
    public void exitData_value_selection(QueryParser.Data_value_selectionContext ctx) {
        super.exitData_value_selection(ctx);

        if (ctx.exception == null) {
            query.setSpecifiesDataValue(true);
        } else {
            query.setSpecifiesDataValue(false);
        }
    }

    @Override
    public void exitProperty_selection(QueryParser.Property_selectionContext ctx) {
        super.exitProperty_selection(ctx);

        if (ctx.exception == null) {
            query.setSpecifiesPropertySelection(true);
            query.setProperty(Query.PROPERTY.valueOf(ctx.getText().toUpperCase()));
        } else {
            query.setSpecifiesPropertySelection(false);
        }
    }

    @Override
    public void exitIndex(QueryParser.IndexContext ctx) {
        super.exitIndex(ctx);

        if (ctx.exception == null) {
            query.setSpecifiesDataValueIndex(true);
            query.setIndexOfDataValue(ctx.getText());
        } else {
            query.setSpecifiesDataValueIndex(false);
        }
    }
}
