/*
 * Copyright 2020 HM Revenue & Customs
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

package mongobee.changesets;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@ChangeLog
public class CacheChangelog {
    private String collection = "cache";

    @ChangeSet(order = "001", id = "External Movements DB Baseline", author = "Paulo Monteiro")
    public void dbBaseline(DB db) {
    }

    @ChangeSet(order = "002", id = "Add 'updated' field to cache and ttl of 60 mins", author = "Steve Sugden")
    public void addCacheUpdateField(MongoDatabase db) {

        Document query = new Document();
        Document update = new Document("$set", new Document("updated", Date.from(Instant.now())));
        db.getCollection(collection).updateMany(new BasicDBObject(query), new BasicDBObject(update));

        IndexOptions options = new IndexOptions().expireAfter(60L, TimeUnit.MINUTES).name("ttl");
        db.getCollection(collection).createIndex(Indexes.ascending("updated"), options);
    }
}