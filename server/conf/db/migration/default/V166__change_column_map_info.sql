
alter table map_info drop exboss_flag, add required_defeat_count int after defeat_count, add gauge_type int after required_defeat_count, add gauge_num int after gauge_type, add air_base_decks int after gauge_num;
