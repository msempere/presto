/*
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
package io.prestosql.sql.tree;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;

public class FetchFirst
        extends Node
{
    private final Optional<String> rowCount;

    public FetchFirst(String rowCount)
    {
        this(Optional.empty(), Optional.of(rowCount));
    }

    public FetchFirst(Optional<String> rowCount)
    {
        this(Optional.empty(), rowCount);
    }

    public FetchFirst(NodeLocation location, Optional<String> rowCount)
    {
        this(Optional.of(location), rowCount);
    }

    public FetchFirst(Optional<NodeLocation> location, Optional<String> rowCount)
    {
        super(location);
        this.rowCount = rowCount;
    }

    public Optional<String> getRowCount()
    {
        return rowCount;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitFetchFirst(this, context);
    }

    @Override
    public List<? extends Node> getChildren()
    {
        return ImmutableList.of();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        FetchFirst o = (FetchFirst) obj;
        return Objects.equals(rowCount, o.rowCount);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(rowCount);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("rowCount", rowCount.orElse(null))
                .omitNullValues()
                .toString();
    }
}
