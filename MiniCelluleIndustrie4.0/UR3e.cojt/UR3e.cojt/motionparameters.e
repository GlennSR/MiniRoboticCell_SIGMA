*V1 - 12-Oct-2015 original version Alex Greenberg
*V2 - 05-Nov-2015 modified by Bernard Hoessler:
*	Replaced entry for joint_config_family j5 to entry for joint_config_family j4
*   Updated default_turns 	

*V3 - 27-01-2016 modified by Bernard Hoessler:
*	J4 config changed
*	Singularity check J5 added

*V4 modified by Alex Greenberg
* added J5 config to ensure unique configuration 

*V5 - 23-04-2019 modified by Radu Simionescu:
* adjusted linear and angular max speeds

*V6 - 11-05-2021 modified by Meir Koren
* joint_motion_by_speed  parameter was added to allow PTP motion simulation to use absolute value instead percentage.

config_family     cf_over_head_pos                         ;
joint_config_family j3    joint_cf_elbow_up         ;
joint_config_family j4    joint_cf_cos_pos                      ;
joint_config_family j5    joint_cf_sin_pos                      ;
default_turns j1 0 0.0, j2 0 0.0, j3 0 0.0, j4 0 0.0, j5 0 0.0, j6 0 0.0;

single_joint_prof single_prof;

singularity_type                joint_range;
singularity_joint j5 0.0 5.0;
check_singularity singularity_check_and_stop;

zone_define fine no_smooth;
zone_define z1 dist cartesian 1;
zone_define z2 dist cartesian 2;
zone_define z10 dist cartesian 10;
zone_define z20 dist cartesian 20;
zone_define z50 dist cartesian 50;
zone_define z100 dist cartesian 100;
zone_define z200 dist cartesian 200;
zone_define z500 dist cartesian 500;
zone_define z1000 dist cartesian 1000;

cart_max_lin_speed 1000;
cart_max_lin_acc 4000;
* 120 deg/s
cart_max_rot_speed 2.0944; 
* 2500 deg/s2
cart_max_rot_acc 43.6332313;

joint_motion_by_speed    no_cnvrt;