/**
 * 
 */
package org.netxms.client;

import java.util.Date;

import org.netxms.base.NXCPCodes;
import org.netxms.base.NXCPMessage;


/**
 * @author Victor
 *
 */
public class NXCAlarm
{
	// Alarm states
	public static final int STATE_OUTSTANDING = 0;
	public static final int STATE_ACKNOWLEDGED = 1;
	public static final int STATE_TERMINATED = 2;
	
	// Alrm helpdesk states
	public static final int HELPDESK_STATE_IGNORED = 0;
	public static final int HELPDESK_STATE_OPEN = 1;
	public static final int HELPDESK_STATE_CLOSED = 2;
	
	// Alarm attributes
	private long id;
	private int currentSeverity;
	private int originalSeverity;
	private int repeatCount;
	private int state;
	private int ackByUser;
	private int terminateByUser;
	private long sourceEventId;
	private int sourceEventCode;
	private long sourceObjectId;
	private Date creationTime;
	private Date lastChangeTime;
	private String message;
	private String key;
	private int helpdeskState;
	private String helpdeskReference;
	private int timeout;
	private int timeoutEvent;
	
	
	/**
	 * @param msg Source NXCP message
	 */
	public NXCAlarm(NXCPMessage msg)
	{
		id = msg.getVariableAsInt64(NXCPCodes.VID_ALARM_ID);
		currentSeverity = msg.getVariableAsInteger(NXCPCodes.VID_CURRENT_SEVERITY);
		originalSeverity = msg.getVariableAsInteger(NXCPCodes.VID_ORIGINAL_SEVERITY);
		repeatCount = msg.getVariableAsInteger(NXCPCodes.VID_REPEAT_COUNT);
		state = msg.getVariableAsInteger(NXCPCodes.VID_STATE);
		ackByUser = msg.getVariableAsInteger(NXCPCodes.VID_ACK_BY_USER);
		terminateByUser = msg.getVariableAsInteger(NXCPCodes.VID_TERMINATED_BY_USER);
		sourceEventId = msg.getVariableAsInt64(NXCPCodes.VID_EVENT_ID);
		sourceEventCode = msg.getVariableAsInteger(NXCPCodes.VID_EVENT_CODE);
		sourceObjectId = msg.getVariableAsInt64(NXCPCodes.VID_OBJECT_ID);
		creationTime = new Date(msg.getVariableAsInt64(NXCPCodes.VID_CREATION_TIME) * 1000);
		lastChangeTime = new Date(msg.getVariableAsInt64(NXCPCodes.VID_LAST_CHANGE_TIME) * 1000);
		message = msg.getVariableAsString(NXCPCodes.VID_ALARM_MESSAGE);
		key = msg.getVariableAsString(NXCPCodes.VID_ALARM_KEY);
		helpdeskState = msg.getVariableAsInteger(NXCPCodes.VID_HELPDESK_STATE);
		helpdeskReference = msg.getVariableAsString(NXCPCodes.VID_HELPDESK_REF);
		timeout = msg.getVariableAsInteger(NXCPCodes.VID_ALARM_TIMEOUT);
		timeoutEvent = msg.getVariableAsInteger(NXCPCodes.VID_ALARM_TIMEOUT_EVENT);
	}

	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return the currentSeverity
	 */
	public int getCurrentSeverity()
	{
		return currentSeverity;
	}

	/**
	 * @return the originalSeverity
	 */
	public int getOriginalSeverity()
	{
		return originalSeverity;
	}

	/**
	 * @return the repeatCount
	 */
	public int getRepeatCount()
	{
		return repeatCount;
	}

	/**
	 * @return the state
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @return the ackByUser
	 */
	public int getAckByUser()
	{
		return ackByUser;
	}

	/**
	 * @return the terminateByUser
	 */
	public int getTerminateByUser()
	{
		return terminateByUser;
	}

	/**
	 * @return the sourceEventId
	 */
	public long getSourceEventId()
	{
		return sourceEventId;
	}

	/**
	 * @return the sourceEventCode
	 */
	public int getSourceEventCode()
	{
		return sourceEventCode;
	}

	/**
	 * @return the sourceObjectId
	 */
	public long getSourceObjectId()
	{
		return sourceObjectId;
	}

	/**
	 * @return the creationTime
	 */
	public Date getCreationTime()
	{
		return creationTime;
	}

	/**
	 * @return the lastChangeTime
	 */
	public Date getLastChangeTime()
	{
		return lastChangeTime;
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * @return the helpdeskState
	 */
	public int getHelpdeskState()
	{
		return helpdeskState;
	}

	/**
	 * @return the helpdeskReference
	 */
	public String getHelpdeskReference()
	{
		return helpdeskReference;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout()
	{
		return timeout;
	}

	/**
	 * @return the timeoutEvent
	 */
	public int getTimeoutEvent()
	{
		return timeoutEvent;
	}
}
