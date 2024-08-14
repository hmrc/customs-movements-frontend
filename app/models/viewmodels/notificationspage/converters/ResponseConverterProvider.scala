/*
 * Copyright 2024 HM Revenue & Customs
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

package models.viewmodels.notificationspage.converters

import javax.inject.{Inject, Singleton}
import models.notifications.Notification
import models.notifications.ResponseType._
import models.viewmodels.decoder.ActionCode._
import models.viewmodels.notificationspage.MovementTotalsResponseType.{EMR, ERS}

import scala.util.{Failure, Success, Try}

@Singleton
class ResponseConverterProvider @Inject() (
  controlResponseAcknowledgedConverter: ControlResponseAcknowledgedConverter,
  controlResponseBlockedConverter: ControlResponseBlockedConverter,
  controlResponseRejectedConverter: ControlResponseRejectedConverter,
  ersResponseConverter: ERSResponseConverter,
  emrResponseConverter: EMRResponseConverter,
  movementResponseConverter: MovementResponseConverter,
  unknownResponseConverter: UnknownResponseConverter
) {

  def provideResponseConverter(notification: Notification): NotificationPageSingleElementConverter =
    getResponseConverter(notification) match {
      case Success(converter) => converter
      case Failure(_)         => unknownResponseConverter
    }

  private def getResponseConverter(notification: Notification): Try[NotificationPageSingleElementConverter] =
    Try(notification.responseType match {
      case MovementTotalsResponse => getMovementTotalsResponseConverter(notification)
      case ControlResponse        => getControlResponseConverter(notification)
      case MovementResponse       => movementResponseConverter
    })

  private def getMovementTotalsResponseConverter(notification: Notification): NotificationPageSingleElementConverter =
    (notification.messageCode: @unchecked) match {
      case ERS.code => ersResponseConverter
      case EMR.code => emrResponseConverter
    }

  private def getControlResponseConverter(notification: Notification): NotificationPageSingleElementConverter =
    notification.actionCode match {
      case Some(AcknowledgedAndProcessed.code)          => controlResponseAcknowledgedConverter
      case Some(PartiallyAcknowledgedAndProcessed.code) => controlResponseBlockedConverter
      case Some(Rejected.code)                          => controlResponseRejectedConverter
      case _                                            => unknownResponseConverter
    }
}
