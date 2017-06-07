package com.markandreydelacruz.nichiyuinventory.models;

/**
 * Created by mark on 5/25/2017.
 */
public class AllRecordsModel {
    private String itemId;
    private String description;
    private String partNumber;
    private String boxNumber;
    private String orderPoint;
    private String quantity;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    public String getOrderPoint() {
        return orderPoint;
    }

    public void setOrderPoint(String orderPoint) {
        this.orderPoint = orderPoint;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
