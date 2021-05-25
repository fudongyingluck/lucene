/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.search.spans;

import java.io.IOException;
import org.apache.lucene.search.DocIdSetIterator;

/**
 * A {@link DocIdSetIterator} which is a disjunction of the approximations of the provided
 * iterators.
 *
 * @lucene.internal
 */
class SpanDisjunctionDISIApproximation extends DocIdSetIterator {

  final SpanDisiPriorityQueue subIterators;
  final long cost;

  public SpanDisjunctionDISIApproximation(SpanDisiPriorityQueue subIterators) {
    this.subIterators = subIterators;
    long cost = 0;
    for (SpanDisiWrapper w : subIterators) {
      cost += w.cost;
    }
    this.cost = cost;
  }

  @Override
  public long cost() {
    return cost;
  }

  @Override
  public int docID() {
    return subIterators.top().doc;
  }

  @Override
  public int nextDoc() throws IOException {
    SpanDisiWrapper top = subIterators.top();
    final int doc = top.doc;
    do {
      top.doc = top.approximation.nextDoc();
      top = subIterators.updateTop();
    } while (top.doc == doc);

    return top.doc;
  }

  @Override
  public int advance(int target) throws IOException {
    SpanDisiWrapper top = subIterators.top();
    do {
      top.doc = top.approximation.advance(target);
      top = subIterators.updateTop();
    } while (top.doc < target);

    return top.doc;
  }
}
