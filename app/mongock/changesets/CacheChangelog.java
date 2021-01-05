/*
 * Copyright 2021 HM Revenue & Customs
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

package mongock.changesets;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;

@ChangeLog
public class CacheChangelog {
    private String collection = "cache";

    @ChangeSet(order = "001", id = "Movements DB Baseline", author = "Steve Sugden")
    public void dbBaseline(MongoDatabase db) {
    }

    @ChangeSet(order = "002", id = "Add updated timestamp for existing cache objects", author = "Steve Sugden")
    public void addUpdatedField(MongoDatabase db) {
        Document query = new Document();
        Document update = new Document("$set", new Document("updated", Date.from(Instant.now())));
        db.getCollection(collection).updateMany(new BasicDBObject(query), new BasicDBObject(update));
    }
}