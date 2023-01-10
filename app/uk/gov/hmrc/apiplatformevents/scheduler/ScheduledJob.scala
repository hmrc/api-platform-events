/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformevents.scheduler

import scala.concurrent.Future

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import uk.gov.hmrc.apiplatformevents.scheduler.SchedulingActor.ScheduledMessage
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger

trait ScheduledJob extends ApplicationLogger {

  val scheduledMessage: ScheduledMessage[_]
  val config: Configuration
  val actorSystem: ActorSystem
  val jobName: String

  val applicationLifecycle: ApplicationLifecycle

  lazy val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  lazy val schedulingActorRef: ActorRef = actorSystem.actorOf(SchedulingActor.props)

  lazy val enabled: Boolean = config.getOptional[Boolean](s"schedules.$jobName.enabled").getOrElse(false)

  lazy val description: Option[String] = config.getOptional[String](s"schedules.$jobName.description")

  lazy val expression: String = config.getOptional[String](s"schedules.$jobName.expression") map (_.replaceAll("_", " ")) getOrElse ""

  lazy val schedule: Unit = {
    (enabled, expression.nonEmpty) match {
      case (true, true)  =>
        scheduler.createSchedule(jobName, description, expression)
        scheduler.schedule(jobName, schedulingActorRef, scheduledMessage)
        logger.error(s"Scheduler for $jobName has been started")
      case (true, false) =>
        logger.error(s"Scheduler for $jobName is disabled as there is no quartz expression")
      case (false, _)    =>
        logger.error(s"Scheduler for $jobName is disabled by configuration")
    }
  }

  applicationLifecycle.addStopHook { () =>
    Future.successful(scheduler.cancelJob(jobName))
    Future.successful(scheduler.shutdown(waitForJobsToComplete = false))
  }
}
