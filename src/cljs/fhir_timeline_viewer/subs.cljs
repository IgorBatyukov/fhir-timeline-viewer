(ns fhir-timeline-viewer.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::loading?
  (fn [db]
    (:loading? db)))


(rf/reg-sub
  ::error
  (fn [db]
    (:error db)))


(rf/reg-sub
  ::timeline-items
  (fn [db]
    (:timeline-items db)))


(rf/reg-sub
  ::selected-item
  (fn [db]
    (:current-item-detail db)))


(rf/reg-sub
  ::selected-item-id
  (fn [db]
    (:selected-item-id db)))


(rf/reg-sub
  ::loading-detail?
  (fn [db]
    (:loading-detail? db)))


(rf/reg-sub
  ::item-detail-error
  (fn [db]
    (:item-detail-error db)))


(rf/reg-sub
  ::timeline-scroll-position
  (fn [db]
    (:timeline-scroll-position db)))
