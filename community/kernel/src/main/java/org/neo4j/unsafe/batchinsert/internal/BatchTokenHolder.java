/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.unsafe.batchinsert.internal;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.storageengine.api.Token;

class BatchTokenHolder
{
    private final Map<String, Token> nameToToken = new HashMap<>();
    private final MutableIntObjectMap<Token> idToToken = new IntObjectHashMap<>( 20 );

    BatchTokenHolder( List<? extends Token> tokens )
    {
        for ( Token token : tokens )
        {
            addToken( token );
        }
    }

    void addToken( Token token )
    {
        nameToToken.put( token.name(), token );
        idToToken.put( token.id(), token );
    }

    Token byId( int id )
    {
        return idToToken.get( id );
    }

    Token byName( String name )
    {
        return nameToToken.get( name );
    }
}
