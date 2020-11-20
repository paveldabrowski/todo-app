-- drop table if exists projects;
create table project_steps(
    id int primary key auto_increment,
    description varchar(100) not NULL,
    project_id  int null,
    days_to_deadline date,
    foreign key (project_id) references PROJECTS(id)
);