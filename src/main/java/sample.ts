import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Client, StompConfig } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { api_url } from 'src/global_constants';
import { CcorDigitalLibraryStartUpService } from './ccor-digital-library-startup.service';
import { v4 as uuidv4 } from 'uuid';

@Injectable()
export class WebSocketService {
  private ws;
  private stompClient: Client;
  private messageSubject: Subject<string> = new Subject<string>();
  private url = `${api_url}/api/search/chat`;
  private headers;
  private clientSubs;
  private showLogs = false;
  private isConnected = false; // Flag to track connection status
  private messageQueue: any[] = []; // Queue to store messages
  private reconnectAttempts = 0; // Track reconnection attempts

  constructor(private startUpService: CcorDigitalLibraryStartUpService) {
    this.headers = {
      LOGIN_USER_ID: this.startUpService.getUser()?.sid,
      LOGIN_USER_NAME: this.startUpService.getUser()?.sid,
      USER_ROLES: this.startUpService.getUser()?.roles,
      APP_CALL_TYPE: this.startUpService.CALL_TYPE,
      X_APP_TRACE_ID: uuidv4()
    };

    this.showLogs = this.startUpService.getUser()?.profile?.some((item) => item.roleNm.indexOf('Admin') !== -1);
  }

  connectClient(payload, publish = true) {
    let shouldPublish = publish;

    this.stompClient = new Client(<StompConfig>{
      reconnectDelay: 3000,
      heartbeatIncoming: 4000, // Heartbeat interval for incoming messages
      heartbeatOutgoing: 4000, // Heartbeat interval for outgoing messages
      webSocketFactory: () => {
        this.ws = new SockJS(this.url, null, { headers: this.headers });
        return this.ws;
      },
      connectHeaders: this.headers,
      onConnect: (frame) => {
        this.isConnected = true; // Set the flag to true when connected
        this.reconnectAttempts = 0; // Reset attempts on successful connection
        const sessionId = this.getSessionId();
        this.clientSubs = this.stompClient.subscribe('/user/' + sessionId + '/queue/messages', message => this.onMessageReceive(message, sessionId));

        // Send queued messages
        this.sendQueuedMessages();

        if (shouldPublish) {
          this.publish(payload);
          shouldPublish = false;
        }
      },
      onDisconnect: (frame) => {
        this.isConnected = false; // Reset the flag on disconnect
        console.log('Disconnected');
        this.scheduleReconnect(); // Schedule reconnection
      },
      onStompError: (frame) => {
        this.isConnected = false; // Reset the flag on error
        console.log('Error: ' + frame.headers['message']);
        console.log('Additional details: ' + frame.body);
        this.scheduleReconnect(); // Schedule reconnection
      },
      debug: (e) => {
        // console.log(e);
      }
    });

    this.activate();
  }

  getSessionId() {
    const url = this.ws._transport.url;
    const lastSlashIndex = url.lastIndexOf('/');
    const sessionId = url.substring(lastSlashIndex + 1, url.length);
    console.log('Stomp Session Id ' + sessionId);
    return sessionId;
  }

  publish(payload) {
    try {
      if (!this.isConnected) {
        console.error('Cannot publish: STOMP client is not connected. Message queued.');
        this.messageQueue.push(payload); // Queue the message
        return;
      }

      if (this.showLogs) {
        console.log("Sent message at " + this.getTime());
      }

      this.stompClient.publish({ destination: '/app/chatbot', body: JSON.stringify(payload) });
    } catch (error) {
      console.error('Publish error:', error);
      this.isConnected = false; // Update connection status on error
      this.messageQueue.push(payload); // Queue the message
    }
  }

  sendQueuedMessages() {
    while (this.messageQueue.length > 0 && this.isConnected) {
      const payload = this.messageQueue.shift();
      this.publish(payload);
    }
  }

  onMessageReceive(message, sessionId) {
    if (this.showLogs) {
      console.log('Received message body at:', this.getTime(), 'message --->', sessionId, message.body);
    }
    this.messageSubject.next(message.body);
  }

  deactivate() {
    if (this.clientSubs) {
      this.clientSubs.unsubscribe();
    }

    if (this.stompClient) {
      console.log('Disconnecting...');
      this.stompClient.deactivate();
    }
  }

  activate() {
    this.stompClient.activate();
  }

  getMessages() {
    return this.messageSubject.asObservable();
  }

  getTime() {
    const now = new Date();
    return now.toLocaleTimeString('en-US', { hour: 'numeric', minute: 'numeric', second: "numeric" });
  }

  private scheduleReconnect() {
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000); // Exponential backoff with max delay
    setTimeout(() => {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect... (Attempt ${this.reconnectAttempts})`);
      this.activate(); // Attempt to reconnect
    }, delay);
  }
}
