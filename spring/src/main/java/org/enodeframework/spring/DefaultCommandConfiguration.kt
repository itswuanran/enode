package org.enodeframework.spring

import org.enodeframework.commanding.CommandConfiguration

/**
 * @author anruence@gmail.com
 */
class DefaultCommandConfiguration(override var host: String, override var port: Int) :
    CommandConfiguration {
}