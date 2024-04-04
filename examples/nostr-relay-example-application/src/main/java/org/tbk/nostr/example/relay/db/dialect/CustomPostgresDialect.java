package org.tbk.nostr.example.relay.db.dialect;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.SqlTypes;

import java.util.List;

public class CustomPostgresDialect extends PostgreSQLDialect {
    public static final String FUNC_TSVECTOR_MATCH = "tsvector_match";

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        functionContributions.getFunctionRegistry().register(FUNC_TSVECTOR_MATCH, new TsVectorMatchExpression(FUNC_TSVECTOR_MATCH));
    }

    public static class TsVectorMatchExpression extends StandardSQLFunction {
        private static final BasicTypeReference<Boolean> RETURN_TYPE = new BasicTypeReference<>("boolean", Boolean.class, SqlTypes.BOOLEAN);

        public TsVectorMatchExpression(String name) {
            super(name, true, RETURN_TYPE);
        }

        @Override
        public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException(
                        "Function '%s' requires 2 arguments".formatted(getName())
                );
            }

            //select p.* from products p where (p.tags @> '{"electronics"}');
            // search_column @@ websearch_to_tsquery('english', ${escapedSearch})
            sqlAppender.append("(");
            arguments.get(0).accept(translator);
            sqlAppender.append(" @@ ");
            arguments.get(1).accept(translator);
            sqlAppender.append(")");
        }
    }
}
