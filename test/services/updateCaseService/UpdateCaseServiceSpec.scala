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

package services.updateCaseService

import base.SpecBase
import mocks.connectors.MockIvdSubmissionConnector
import mocks.services.MockAuditService
import models.audit.UpdateCaseAuditEvent
import models.requests._
import models.{UpdateCaseError, UpdateCaseResponse, UserAnswers}
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import services.UpdateCaseService
import utils.ReusableValues

class UpdateCaseServiceSpec extends SpecBase {

  trait Test extends MockIvdSubmissionConnector with MockAuditService with UpdateCaseServiceTestData with ReusableValues {
    def setupMock(response: Either[UpdateCaseError, UpdateCaseResponse]): Unit = {
      setupMockUpdateCase(response)
    }

    val userAnswers: UserAnswers = completeUserAnswers

    implicit lazy val dataRequest: DataRequest[AnyContentAsEmpty.type] = DataRequest(
      OptionalDataRequest(
        IdentifierRequest(fakeRequest, "credId", "eori"),
        "credId",
        "eori",
        Some(userAnswers)
      ),
      "credId",
      "eori",
      userAnswers
    )

    val failedCreateCaseConnectorCall: UpdateCaseError =
      UpdateCaseError.UnexpectedError(Status.BAD_REQUEST, Some("Downstream error returned when retrieving SubmissionResponse from back end"))

    val service = new UpdateCaseService(mockIVDSubmissionConnector, mockAuditService)
  }

  "UpdateCaseService" when {
    "called with valid user answers" should {
      "return successful UpdateCaseResponse" in new Test {
        private val response: UpdateCaseResponse = UpdateCaseResponse("1234")
        setupMockUpdateCase(Right(response))
        verifyAudit(UpdateCaseAuditEvent(updateData, "credId", "eori"))
        private val result = await(service.updateCase()(dataRequest, hc, ec))
        result mustBe Right(response)
      }

      "return error if connector call fails" in new Test {
        setupMock(Left(failedCreateCaseConnectorCall))
        private val result = await(service.updateCase()(dataRequest, hc, ec))

        result mustBe Left(failedCreateCaseConnectorCall)
      }
    }

    "called with incomplete User Answers" should {
      "return error - unable to parse to model" in new Test {
        override val userAnswers: UserAnswers = UserAnswers("some-cred-id")
        private val result = await(service.updateCase()(dataRequest, hc, ec))

        result must matchPattern {
          case Left(UpdateCaseError.UnexpectedError(_, _)) =>
        }
      }
    }
  }

  "buildUpdate" when {
    "called with a complete User Answers" should {
      "return expected JSON" in new Test {
        private val result = service.buildUpdate(updateData)

        result mustBe updateCaseJson
      }
    }

    "called without supporting documents" should {
      "return expected JSON" in new Test {
        private val result = service.buildUpdate(updateData.copy(supportingDocuments = None))

        result mustBe updateCaseJsonWithoutDocs
      }
    }
  }
}
