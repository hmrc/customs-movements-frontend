/*
 * Copyright 2019 HM Revenue & Customs
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
import models.notifications.NotificationFrontendModel
import models.notifications.ResponseType._
import models.viewmodels.notificationspage.MovementTotalsResponseType.{EMR, ERS}

import scala.util.{Failure, Success, Try}

@Singleton
class ResponseConverterProvider @Inject()(
  controlResponseConverter: ControlResponseConverter,
  ersResponseConverter: ERSResponseConverter,
  emrResponseConverter: EMRResponseConverter,
  movementResponseConverter: MovementResponseConverter,
  unknownResponseConverter: UnknownResponseConverter
) {

  def provideResponseConverter(notification: NotificationFrontendModel): NotificationPageSingleElementConverter =
    getResponseConverter(notification) match {
      case Success(converter) => converter
      case Failure(_)         => unknownResponseConverter
    }

  private def getResponseConverter(
    notification: NotificationFrontendModel
  ): Try[NotificationPageSingleElementConverter] =
    Try(notification.responseType match {
      case MovementTotalsResponse => getMovementTotalsResponseConverter(notification)
      case ControlResponse        => controlResponseConverter
      case MovementResponse       => movementResponseConverter
    })

  private def getMovementTotalsResponseConverter(
    notification: NotificationFrontendModel
  ): NotificationPageSingleElementConverter = notification.messageCode match {
    case ERS.code => ersResponseConverter
    case EMR.code => emrResponseConverter
  }

}
