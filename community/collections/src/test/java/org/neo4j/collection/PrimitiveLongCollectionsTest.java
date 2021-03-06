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
package org.neo4j.collection;

import org.eclipse.collections.api.iterator.LongIterator;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.iterator.ImmutableEmptyLongIterator;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.neo4j.collection.PrimitiveLongCollections.PrimitiveLongBaseIterator;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PrimitiveLongCollectionsTest
{
    @Test
    public void arrayOfItemsAsIterator()
    {
        // GIVEN
        long[] items = new long[] { 2, 5, 234 };

        // WHEN
        LongIterator iterator = PrimitiveLongCollections.iterator( items );

        // THEN
        assertItems( iterator, items );
    }

    @Test
    public void filter()
    {
        // GIVEN
        LongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        LongIterator filtered = PrimitiveLongCollections.filter( items, item -> item != 2 );

        // THEN
        assertItems( filtered, 1, 3 );
    }

    private static final class CountingPrimitiveLongIteratorResource implements LongIterator, AutoCloseable
    {
        private final LongIterator delegate;
        private final AtomicInteger closeCounter;

        private CountingPrimitiveLongIteratorResource( LongIterator delegate, AtomicInteger closeCounter )
        {
            this.delegate = delegate;
            this.closeCounter = closeCounter;
        }

        @Override
        public void close()
        {
            closeCounter.incrementAndGet();
        }

        @Override
        public boolean hasNext()
        {
            return delegate.hasNext();
        }

        @Override
        public long next()
        {
            return delegate.next();
        }
    }

    @Test
    public void singleWithDefaultMustAutoCloseIterator()
    {
        AtomicInteger counter = new AtomicInteger();
        CountingPrimitiveLongIteratorResource itr = new CountingPrimitiveLongIteratorResource(
                PrimitiveLongCollections.iterator( 13 ), counter );
        assertEquals( PrimitiveLongCollections.single( itr, 2 ), 13 );
        assertEquals( 1, counter.get() );
    }

    @Test
    public void singleWithDefaultMustAutoCloseEmptyIterator()
    {
        AtomicInteger counter = new AtomicInteger();
        CountingPrimitiveLongIteratorResource itr = new CountingPrimitiveLongIteratorResource(
                ImmutableEmptyLongIterator.INSTANCE, counter );
        assertEquals( PrimitiveLongCollections.single( itr, 2 ), 2 );
        assertEquals( 1, counter.get() );
    }

    @Test
    public void indexOf()
    {
        // GIVEN
        Supplier<LongIterator> items = () -> PrimitiveLongCollections.iterator( 10, 20, 30 );

        // THEN
        assertEquals( -1, PrimitiveLongCollections.indexOf( items.get(), 55 ) );
        assertEquals( 0, PrimitiveLongCollections.indexOf( items.get(), 10 ) );
        assertEquals( 1, PrimitiveLongCollections.indexOf( items.get(), 20 ) );
        assertEquals( 2, PrimitiveLongCollections.indexOf( items.get(), 30 ) );
    }

    @Test
    public void count()
    {
        // GIVEN
        LongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        int count = PrimitiveLongCollections.count( items );

        // THEN
        assertEquals( 3, count );
    }

    @Test
    public void asArray()
    {
        // GIVEN
        LongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        long[] array = PrimitiveLongCollections.asArray( items );

        // THEN
        assertTrue( Arrays.equals( new long[] { 1, 2, 3 }, array ) );
    }

    @Test
    public void shouldDeduplicate()
    {
        // GIVEN
        long[] array = new long[] {1L, 1L, 2L, 5L, 6L, 6L};

        // WHEN
        long[] deduped = PrimitiveLongCollections.deduplicate( array );

        // THEN
        assertArrayEquals( new long[] {1L, 2L, 5L, 6L}, deduped );
    }

    @Test
    public void shouldNotContinueToCallNextOnHasNextFalse()
    {
        // GIVEN
        AtomicLong count = new AtomicLong( 2 );
        LongIterator iterator = new PrimitiveLongBaseIterator()
        {
            @Override
            protected boolean fetchNext()
            {
                return count.decrementAndGet() >= 0 && next( count.get() );
            }
        };

        // WHEN/THEN
        assertTrue( iterator.hasNext() );
        assertTrue( iterator.hasNext() );
        assertEquals( 1L, iterator.next() );
        assertTrue( iterator.hasNext() );
        assertTrue( iterator.hasNext() );
        assertEquals( 0L, iterator.next() );
        assertFalse( iterator.hasNext() );
        assertFalse( iterator.hasNext() );
        assertEquals( -1L, count.get() );
    }

    @Test
    public void convertJavaCollectionToSetOfPrimitives()
    {
        List<Long> longs = asList( 1L, 4L, 7L );
        LongSet longSet = PrimitiveLongCollections.asSet( longs );
        assertTrue( longSet.contains( 1L ) );
        assertTrue( longSet.contains( 4L ) );
        assertTrue( longSet.contains( 7L ) );
        assertEquals( 3, longSet.size() );
    }

    @Test
    public void convertPrimitiveSetToJavaSet()
    {
        LongSet longSet = LongHashSet.newSetWith( 1L, 3L, 5L );
        Set<Long> longs = PrimitiveLongCollections.toSet( longSet );
        assertThat( longs, containsInAnyOrder(1L, 3L, 5L) );
    }

    private void assertNoMoreItems( LongIterator iterator )
    {
        assertFalse( iterator + " should have no more items", iterator.hasNext() );
        try
        {
            iterator.next();
            fail( "Invoking next() on " + iterator +
                    " which has no items left should have thrown NoSuchElementException" );
        }
        catch ( NoSuchElementException e )
        {   // Good
        }
    }

    private void assertNextEquals( long expected, LongIterator iterator )
    {
        assertTrue( iterator + " should have had more items", iterator.hasNext() );
        assertEquals( expected, iterator.next() );
    }

    private void assertItems( LongIterator iterator, long... expectedItems )
    {
        for ( long expectedItem : expectedItems )
        {
            assertNextEquals( expectedItem, iterator );
        }
        assertNoMoreItems( iterator );
    }

    private long[] reverse( long[] items )
    {
        long[] result = new long[items.length];
        for ( int i = 0; i < items.length; i++ )
        {
            result[i] = items[items.length - i - 1];
        }
        return result;
    }
}
