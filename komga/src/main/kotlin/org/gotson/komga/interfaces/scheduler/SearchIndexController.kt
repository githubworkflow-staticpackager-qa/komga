package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.application.tasks.HIGHEST_PRIORITY
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.infrastructure.search.LuceneHelper
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class SearchIndexController(
  private val luceneHelper: LuceneHelper,
  private val taskEmitter: TaskEmitter,
) {

  @EventListener(ApplicationReadyEvent::class)
  fun createIndexIfNoneExist() {
    if (!luceneHelper.indexExists()) {
      logger.info { "Lucene index not found, trigger rebuild" }
      taskEmitter.rebuildIndex(HIGHEST_PRIORITY)
    } else {
      logger.info { "Lucene index version: ${luceneHelper.getIndexVersion()}" }
      when {
        luceneHelper.getIndexVersion() < 6 -> taskEmitter.upgradeIndex(HIGHEST_PRIORITY) // upgrade index to Lucene 9.x
      }
    }
  }
}
