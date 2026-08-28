package org.uestc.weglas.base.dal.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RoomEntity {
    private String id;
    private String roomCode;
    private String roomName;
    private String building;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
