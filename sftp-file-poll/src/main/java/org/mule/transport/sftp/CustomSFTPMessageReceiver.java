/*
 * $Id: SftpMessageReceiver.java 24026 2012-03-13 17:34:29Z pablo.lagreca $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.sftp;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.execution.ExecutionCallback;
import org.mule.api.execution.ExecutionTemplate;
import org.mule.api.lifecycle.CreateException;
import org.mule.transport.AbstractPollingMessageReceiver;
import org.mule.transport.sftp.notification.SftpNotifier;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.execution.ExecutionCallback;
import org.mule.api.execution.ExecutionTemplate;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.construct.Flow;
import org.mule.processor.strategy.SynchronousProcessingStrategy;
import org.mule.transport.AbstractPollingMessageReceiver;
import org.mule.transport.sftp.SftpConnector;
import org.mule.transport.sftp.SftpInputStream;
import org.mule.transport.sftp.SftpReceiverRequesterUtil;
import org.mule.transport.sftp.notification.SftpNotifier;
import org.mule.util.IOUtils;
import org.mule.util.lock.LockFactory; 



import java.io.InputStream;
import java.util.Arrays;

/**
 * <code>SftpMessageReceiver</code> polls and receives files from an sftp service
 * using jsch. This receiver produces an InputStream payload, which can be
 * materialized in a MessageDispatcher or Component.
 */
public class CustomSFTPMessageReceiver extends AbstractPollingMessageReceiver
{

    private CustomSftpReceiverRequesterUtil sftpRRUtil = null;

    public CustomSFTPMessageReceiver(SftpConnector connector,
			FlowConstruct flowConstruct, InboundEndpoint endpoint,
			long frequency) throws CreateException {
    	super(connector, flowConstruct, endpoint);   	

        this.setFrequency(frequency);

        sftpRRUtil = new CustomSftpReceiverRequesterUtil(endpoint);
    }

    public void poll() throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Pooling. Called at endpoint " + endpoint.getEndpointURI());
        }
        try
        {
        	String[] files = sftpRRUtil.getAvailableFiles(false);

            if (files.length == 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Pooling. No matching files found at endpoint " + endpoint.getEndpointURI());
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Pooling. " + files.length + " files found at " + endpoint.getEndpointURI()
                                 + ":" + Arrays.toString(files));
                }
                for (String file : files)
                {
                    if (getLifecycleState().isStopping())
                    {
                        break;
                    }
                    routeFile(file);
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("Pooling. Routed all " + files.length + " files found at "
                                 + endpoint.getEndpointURI());
                }
            }
        }
        catch (MessagingException e)
        {
            //Already handled by TransactionTemplate
        }
        catch (Exception e)
        {
            logger.error("Error in poll", e);
            connector.getMuleContext().getExceptionListener().handleException(e);
            throw e;
        }
    }

    @Override
    protected boolean pollOnPrimaryInstanceOnly()
    {
        return true;
    }

    protected void routeFile(final String path) throws Exception
    {
        ExecutionTemplate<MuleEvent> executionTemplate = createExecutionTemplate();
        executionTemplate.execute(new ExecutionCallback<MuleEvent>()
        {
            @Override
            public MuleEvent process() throws Exception
            {
                // A bit tricky initialization of the notifier in this case since we don't
                // have access to the message yet...
                SftpNotifier notifier = new SftpNotifier((SftpConnector) connector, createNullMuleMessage(),
                        endpoint, flowConstruct.getName());

                InputStream inputStream = sftpRRUtil.retrieveFile(path, notifier);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Routing file: " + path);
                }

                MuleMessage message = createMuleMessage(inputStream);
                logger.info("message created");

                message.setOutboundProperty(SftpConnector.PROPERTY_FILENAME, path);
                message.setOutboundProperty(SftpConnector.PROPERTY_ORIGINAL_FILENAME, path);

                // Now we have access to the message, update the notifier with the message
                notifier.setMessage(message);
                routeMessage(message);

                logger.info("Routed file: " + path);
                return null;
            }
        });
    }

    /**
     * SFTP-35
     */
    @Override 
    protected MuleMessage handleUnacceptedFilter(MuleMessage message) {
        logger.debug("the filter said no, now trying to close the payload stream");
        try {
            final SftpInputStream payload = (SftpInputStream) message.getPayload();
            payload.close();
        }
        catch (Exception e) {
            logger.debug("unable to close payload stream", e);
        }
        return super.handleUnacceptedFilter(message);
    }

    public void doConnect() throws Exception
    {
        // no op
    }

    public void doDisconnect() throws Exception
    {
        // no op
    }

    protected void doDispose()
    {
        // no op
    }
}