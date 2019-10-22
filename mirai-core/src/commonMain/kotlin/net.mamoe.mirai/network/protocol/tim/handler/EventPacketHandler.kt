package net.mamoe.mirai.network.protocol.tim.handler

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.getGroupByNumber
import net.mamoe.mirai.getQQ
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.distributePacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerFriendOnlineStatusChangedPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendFriendMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendGroupMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.event.IgnoredServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerFriendMessageEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerGroupMessageEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerGroupUploadFileEventPacket

/**
 * 处理消息事件, 承担消息发送任务.
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class EventPacketHandler(session: BotSession) : PacketHandler(session) {
    companion object Key : PacketHandler.Key<EventPacketHandler>

    override suspend fun onPacketReceived(packet: ServerPacket): Unit = with(session) {
        when (packet) {
            is ServerGroupUploadFileEventPacket -> {
                //todo
            }

            is ServerFriendMessageEventPacket -> {
                if (!packet.isPrevious) {
                    FriendMessageEvent(bot, bot.getQQ(packet.qq), packet.message).broadcast()
                }
            }

            is ServerGroupMessageEventPacket -> {
                if (packet.qq == bot.account.account) return

                GroupMessageEvent(
                        bot,
                        group = bot.getGroupByNumber(packet.groupNumber),
                        sender = bot.getQQ(packet.qq),
                        message = packet.message,
                        senderName = packet.senderName,
                        senderPermission = packet.senderPermission
                ).broadcast()
            }

            is ServerFriendOnlineStatusChangedPacket.Encrypted -> distributePacket(packet.decrypt(sessionKey))
            is ServerFriendOnlineStatusChangedPacket -> {
                //TODO
            }

            is IgnoredServerEventPacket -> {

            }
            else -> {
                //ignored
            }
        }
    }

    suspend fun sendFriendMessage(qq: QQ, message: MessageChain) {
        session.socket.sendPacket(SendFriendMessagePacket(session.bot.account.account, qq.number, session.sessionKey, message))
    }

    suspend fun sendGroupMessage(group: Group, message: MessageChain) {
        session.socket.sendPacket(SendGroupMessagePacket(session.bot.account.account, group.groupId, session.sessionKey, message))
    }
}